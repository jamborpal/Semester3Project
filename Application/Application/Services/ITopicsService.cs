﻿using System.Collections.Generic;
using System.Threading.Tasks;
using Application.Models;

namespace Application.Services
{
    public interface ITopicsService
    {
        public Task AddTopic(Topic topic);
        public Task RemoveTopic(Topic topic);
        public Task<IList<Topic>> GetAllTopics();
        public Task<Topic> GetTopicById(string id);
    }
}