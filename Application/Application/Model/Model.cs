﻿﻿using System.Collections.Generic;
using System.Threading.Tasks;
 using Application.Model;
 using MongoDB.Bson;

namespace Tier2.Model
{
    public interface Model
    {
        public Task RequestChatrooms();
        public Task RequestUsers();
        public Task RequestTopics();
        public Task SendMessage(Message message, string chatroomId);
        public Task Register(Account account);
        public void ProcessCredentials(string credentialsJson);
        public void ProcessChatrooms(string credentialsJson);
        public void ProcessTopics(string credentialsJson);
    }
}