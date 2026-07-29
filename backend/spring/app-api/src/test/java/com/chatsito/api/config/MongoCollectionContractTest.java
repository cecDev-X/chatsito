package com.chatsito.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.chatsito.api.chat.MessageDocument;
import com.chatsito.api.chat.UnreadMessageDocument;
import com.chatsito.api.notification.NotificationDocument;
import com.chatsito.api.post.CommentDocument;
import com.chatsito.api.post.PostDocument;
import com.chatsito.api.user.UserDocument;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

class MongoCollectionContractTest {
    @Test
    void usesTheCollectionAndIndexNamesPresentInTheLegacySocialDatabase() throws Exception {
        assertCollection(UserDocument.class, "User");
        assertCollection(PostDocument.class, "Post");
        assertCollection(CommentDocument.class, "Comment");
        assertCollection(MessageDocument.class, "Message");
        assertCollection(UnreadMessageDocument.class, "UnReadedMsg");
        assertCollection(NotificationDocument.class, "Notification");
        var emailIndex = UserDocument.class.getDeclaredField("email").getAnnotation(Indexed.class);
        assertThat(emailIndex.name()).isEqualTo("email_1");
        assertThat(emailIndex.unique()).isTrue();
    }

    private void assertCollection(Class<?> documentType, String expectedName) {
        assertThat(documentType.getAnnotation(Document.class).collection()).isEqualTo(expectedName);
    }
}
