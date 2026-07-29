#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SPRING_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:5000}"
MONGODB_URI="${MONGODB_URI:-mongodb://localhost:27017/social_spring_test}"
MAIN_ID="000000000000000000000001"
SUGGESTION_ID="000000000000000000000004"
POST_ID="300000000000000000000002"
checks=0
RESPONSE=""

seed() {
  mongosh --quiet "$MONGODB_URI" "$SCRIPT_DIR/seed-local-verification.js"
}

restore_fixture() {
  seed >/dev/null
  printf 'Fixture restored: social_spring_test\n'
}

trap restore_fixture EXIT

request() {
  local method="$1"
  local path="$2"
  local expected_status="$3"
  local body="${4:-}"
  local token="${5:-}"
  local output
  local status
  local -a args=(--silent --show-error --request "$method" "$BASE_URL$path" --write-out $'\n%{http_code}')

  if [[ -n "$token" ]]; then
    args+=(--header "Authorization: Bearer $token")
  fi
  if [[ -n "$body" ]]; then
    args+=(--header 'Content-Type: application/json' --data "$body")
  fi

  output="$(curl "${args[@]}")"
  status="${output##*$'\n'}"
  RESPONSE="${output%$'\n'*}"
  if [[ "$status" != "$expected_status" ]]; then
    printf 'Expected HTTP %s from %s %s, received %s: %s\n' \
      "$expected_status" "$method" "$path" "$status" "$RESPONSE" >&2
    exit 1
  fi
}

assert_response() {
  local endpoint="$1"
  local filter="$2"
  if ! jq --exit-status "$filter" <<<"$RESPONSE" >/dev/null; then
    printf 'Contract assertion failed for %s: %s\nResponse: %s\n' \
      "$endpoint" "$filter" "$RESPONSE" >&2
    exit 1
  fi
  checks=$((checks + 1))
  printf '[%02d/22] %s\n' "$checks" "$endpoint"
}

for command in curl jq mongosh; do
  if ! command -v "$command" >/dev/null; then
    printf 'Required command not found: %s\n' "$command" >&2
    exit 1
  fi
done

request GET /actuator/health 200
jq --exit-status '.status == "UP"' <<<"$RESPONSE" >/dev/null
seed >/dev/null

request POST /user/signin 200 \
  '{"email":"main@spring.test","password":"spring-password"}'
MAIN_TOKEN="$(jq -r '.token' <<<"$RESPONSE")"
assert_response 'POST /user/signin' \
  '.result.id == "000000000000000000000001" and (.token | length > 20)'

request GET "/user/getUser/$MAIN_ID" 200
assert_response 'GET /user/getUser/{id}' \
  '.user.name == "Spring Main" and .posts == "posts"'

request GET "/user/getSug?id=$MAIN_ID" 200
assert_response 'GET /user/getSug' \
  '(.users | length == 1) and .users[0].name == "Visible Suggestion"'

request PATCH "/user/Update/$MAIN_ID" 200 \
  '{"name":"Spring Updated","bio":"Updated bio","imageUrl":"updated.png","email":"tampered@example.com","password":"tampered","followers":["tampered"],"following":["tampered"]}' \
  "$MAIN_TOKEN"
assert_response 'PATCH /user/Update/{id}' \
  '.user.name == "Spring Updated" and .user.email == "main@spring.test" and .user.followers == ["000000000000000000000003"] and .user.following == ["000000000000000000000002"]'

request PATCH "/user/$SUGGESTION_ID/following" 200 "" "$MAIN_TOKEN"
assert_response 'PATCH /user/{id}/following' \
  '.updateduser1.followers == ["000000000000000000000001"] and (.updateduser2.following | index("000000000000000000000004") != null)'

request POST /user/signup 201 \
  '{"firstName":"Smoke","lastName":"Delete","email":"smoke.delete@spring.test","password":"compatible-password"}'
DELETE_ID="$(jq -r '.result.id' <<<"$RESPONSE")"
DELETE_TOKEN="$(jq -r '.token' <<<"$RESPONSE")"
assert_response 'POST /user/signup' \
  '.result.name == "Smoke Delete" and (.token | length > 20)'

request GET '/posts?page=1' 200
assert_response 'GET /posts' \
  '(.data | length == 6) and .currentPage == 1 and .numberOfPages == 2'

request GET '/posts/search?searchQuery=visible' 200
assert_response 'GET /posts/search' \
  '(.data.user | length == 1) and (.data.posts | length == 2)'

request GET '/posts/300000000000000000000007' 200
assert_response 'GET /posts/{id}' \
  '.post.title == "Suggestion Profile Post" and .post.comments[0].value == "Fixture comment"'

request POST /posts 201 \
  '{"message":"Smoke create","selectedFile":"","title":"Smoke Post","creator":"000000000000000000000004"}' \
  "$MAIN_TOKEN"
CREATED_POST_ID="$(jq -r '._id' <<<"$RESPONSE")"
assert_response 'POST /posts' \
  '.title == "Smoke Post" and .creator == "000000000000000000000001" and .likes == []'

request PATCH "/posts/$CREATED_POST_ID" 200 \
  '{"message":"Smoke updated","selectedFile":"updated.png","title":"Smoke Updated Post","creator":"tampered","likes":["tampered"]}' \
  "$MAIN_TOKEN"
assert_response 'PATCH /posts/{id}' \
  '.data.title == "Smoke Updated Post" and .data.creator == "000000000000000000000001" and .data.likes == []'

request PATCH "/posts/$POST_ID/likePost" 200 "" "$MAIN_TOKEN"
assert_response 'PATCH /posts/{id}/likePost' \
  '.likes == ["000000000000000000000001"]'

request DELETE "/posts/$CREATED_POST_ID" 200 "" "$MAIN_TOKEN"
assert_response 'DELETE /posts/{id}' \
  '.message == "post deleted successfully."'

request POST "/posts/$POST_ID/commentPost" 201 \
  '{"value":"Smoke comment"}' "$MAIN_TOKEN"
CREATED_COMMENT_ID="$(jq -r '.post.comments[0]._id' <<<"$RESPONSE")"
assert_response 'POST /posts/{id}/commentPost' \
  '.post.comments[0].value == "Smoke comment" and .post.comments[0].user.name == "Spring Updated"'

request DELETE "/posts/$CREATED_COMMENT_ID/deleteComment" 200 "" "$MAIN_TOKEN"
assert_response 'DELETE /posts/{id}/deleteComment' \
  '.message == "comment deleted successfully."'

request POST /chat/sendmessage 200 \
  '{"content":"Smoke REST fallback","sender":"000000000000000000000001","recever":"000000000000000000000002"}'
assert_response 'POST /chat/sendmessage' \
  '.msg.content == "Smoke REST fallback" and .msg.recever == "000000000000000000000002"'

request GET '/chat/getmsgsbynums?from=0&firstuid=000000000000000000000001&seconduid=000000000000000000000002' 200
assert_response 'GET /chat/getmsgsbynums' \
  '(.msgs | length == 8) and (.msgs | any(.content == "Smoke REST fallback")) and .hasMore == true'

request GET "/chat/get-user-unreadedmsg?userid=$MAIN_ID" 200
assert_response 'GET /chat/get-user-unreadedmsg' \
  '.total == 5 and (.messages | length == 2)'

request GET "/chat/mark-msg-asreaded?mainuid=$MAIN_ID&otheruid=000000000000000000000002" 200
assert_response 'GET /chat/mark-msg-asreaded' '.isMarked == true'

request GET "/notification/$MAIN_ID" 200
assert_response 'GET /notification/{userid}' \
  '(.notifications | length == 3) and .notifications[0].deatils == "Newest Comment"'

request GET "/notification/mark-notification-asreaded?id=$MAIN_ID" 200
assert_response 'GET /notification/mark-notification-asreaded' \
  '.message == "Notification maked as read"'

request DELETE "/user/delete/$DELETE_ID" 200 "" "$DELETE_TOKEN"
assert_response 'DELETE /user/delete/{id}' \
  '.message == "user Delted Successfully."'

printf 'All %d HTTP endpoints passed.\n' "$checks"
