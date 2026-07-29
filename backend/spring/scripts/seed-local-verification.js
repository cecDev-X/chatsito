const database = db.getSiblingDB("social_spring_test");

database.dropDatabase();

const mainId = "000000000000000000000001";
const followingId = "000000000000000000000002";
const followerId = "000000000000000000000003";
const suggestionId = "000000000000000000000004";

database.User.insertMany([
  {
    _id: ObjectId(mainId),
    name: "Spring Main",
    email: "main@spring.test",
    password: "$2b$12$Ss4jJ41I2oxLTFvqzAasieSreOsfyJo2DrlwZapDyWVT8zbFSlvD2",
    bio: "Main test user",
    imageUrl: "main.png",
    followers: [followerId],
    following: [followingId],
  },
  {
    _id: ObjectId(followingId),
    name: "Already Following",
    email: "following@spring.test",
    password: "test-hash",
    bio: "Must be excluded",
    imageUrl: "following.png",
    followers: [],
    following: [],
  },
  {
    _id: ObjectId(followerId),
    name: "Existing Follower",
    email: "follower@spring.test",
    password: "test-hash",
    bio: "Must be excluded",
    imageUrl: "follower.png",
    followers: [],
    following: [],
  },
  {
    _id: ObjectId(suggestionId),
    name: "Visible Suggestion",
    email: "suggestion@spring.test",
    password: "test-hash",
    bio: "Must be returned",
    imageUrl: "suggestion.png",
    followers: [],
    following: [],
  },
]);
database.User.createIndex({ email: 1 }, { unique: true, name: "email_1" });

database.Post.insertMany([
  {
    _id: ObjectId("300000000000000000000001"),
    title: "Visible Spring Post",
    message: "Migration checkpoint",
    creator: mainId,
    selectedFile: "",
    likes: [],
    createdAt: ISODate("2026-07-28T10:00:00Z"),
  },
  {
    _id: ObjectId("300000000000000000000002"),
    title: "Other Post",
    message: "A visible message",
    creator: suggestionId,
    selectedFile: "",
    likes: [],
    createdAt: ISODate("2026-07-28T11:00:00Z"),
  },
  {
    _id: ObjectId("300000000000000000000003"),
    title: "Unrelated Post",
    message: "Nothing matches",
    creator: followingId,
    selectedFile: "",
    likes: [],
    createdAt: ISODate("2026-07-28T12:00:00Z"),
  },
  {
    _id: ObjectId("300000000000000000000004"),
    title: "Main Feed Post",
    message: "Created by main",
    creator: mainId,
    selectedFile: "",
    likes: [followingId],
    createdAt: ISODate("2026-07-28T13:00:00Z"),
  },
  {
    _id: ObjectId("300000000000000000000005"),
    title: "Following Feed Post",
    message: "Created by followed user",
    creator: followingId,
    selectedFile: "",
    likes: [],
    createdAt: ISODate("2026-07-28T14:00:00Z"),
  },
  {
    _id: ObjectId("300000000000000000000006"),
    title: "Follower Only Post",
    message: "Excluded from main feed",
    creator: followerId,
    selectedFile: "",
    likes: [],
    createdAt: ISODate("2026-07-28T15:00:00Z"),
  },
  {
    _id: ObjectId("300000000000000000000007"),
    title: "Suggestion Profile Post",
    message: "Newest global post",
    creator: suggestionId,
    selectedFile: "",
    likes: [mainId],
    createdAt: ISODate("2026-07-28T16:00:00Z"),
  },
]);

database.Comment.insertOne({
  _id: ObjectId("500000000000000000000001"),
  postId: "300000000000000000000007",
  userId: followingId,
  value: "Fixture comment",
  createdAt: ISODate("2026-07-28T17:00:00Z"),
});

database.UnReadedMsg.insertMany([
  {
    _id: ObjectId("100000000000000000000001"),
    mainUserid: mainId,
    otherUserid: followingId,
    numOfUnreadedMessages: 3,
    isReaded: false,
  },
  {
    _id: ObjectId("100000000000000000000002"),
    mainUserid: mainId,
    otherUserid: followerId,
    numOfUnreadedMessages: 2,
    isReaded: false,
  },
  {
    _id: ObjectId("100000000000000000000003"),
    mainUserid: mainId,
    otherUserid: suggestionId,
    numOfUnreadedMessages: 7,
    isReaded: true,
  },
]);

database.Notification.insertMany([
  {
    _id: ObjectId("200000000000000000000001"),
    deatils: "Oldest Follow",
    mainuid: mainId,
    targetid: followingId,
    isreded: false,
    createdAt: ISODate("2026-07-28T10:00:00Z"),
    user: { name: "Already Following", avatar: null },
  },
  {
    _id: ObjectId("200000000000000000000002"),
    deatils: "Middle Like",
    mainuid: mainId,
    targetid: "post-1",
    isreded: true,
    createdAt: ISODate("2026-07-28T11:00:00Z"),
    user: { name: "Existing Follower", avatar: null },
  },
  {
    _id: ObjectId("200000000000000000000003"),
    deatils: "Newest Comment",
    mainuid: mainId,
    targetid: "post-2",
    isreded: false,
    createdAt: ISODate("2026-07-28T12:00:00Z"),
    user: { name: "Visible Suggestion", avatar: null },
  },
  {
    _id: ObjectId("200000000000000000000004"),
    deatils: "Other User",
    mainuid: suggestionId,
    targetid: "post-3",
    isreded: false,
    createdAt: ISODate("2026-07-28T13:00:00Z"),
    user: { name: "Spring Main", avatar: null },
  },
]);

const messages = [];
for (let index = 1; index <= 9; index += 1) {
  messages.push({
    _id: ObjectId(`40000000000000000000000${index}`),
    content: `Fixture message ${index}`,
    sender: index % 2 === 0 ? followingId : mainId,
    recever: index % 2 === 0 ? mainId : followingId,
  });
}
database.Message.insertMany(messages);

printjson({
  database: database.getName(),
  users: database.User.countDocuments(),
  posts: database.Post.countDocuments(),
  comments: database.Comment.countDocuments(),
  unreadedmsg: database.UnReadedMsg.countDocuments(),
  notifications: database.Notification.countDocuments(),
  messages: database.Message.countDocuments(),
});
