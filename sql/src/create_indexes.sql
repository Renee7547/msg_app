CREATE INDEX chat_init
ON chat
USING BTREE
(init_sender);

CREATE INDEX chat_mem
ON chat_list
USING BTREE
(member);

CREATE INDEX mess_sender_chat
ON message
USING BTREE
(sender_login, chat_id);
