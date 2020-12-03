﻿using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Application.Model;
using Application.Services;
using Microsoft.AspNetCore.Mvc;
using Tier2.Model;

namespace Application.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class TopicsController : ControllerBase
    {
        private ITopicsService TopicsService;

        public TopicsController(ITopicsService topicsService)
        {
            this.TopicsService = topicsService;
        }

        [HttpGet]
        public async Task<ActionResult<IList<Topic>>> GetAllTopics()
        {
            try
            {
                IList<Topic> topics = await TopicsService.GetAllTopics();
                return Ok(topics);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return StatusCode(500, e.Message);
            }
        }

        [HttpGet]
        [Route("{id:string}")]
        public async Task<ActionResult<Topic>> GetTopicByID([FromRoute] string id)
        {
            try
            {
                Topic topic = await TopicsService.GetTopicById(id);
                return Ok(topic);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return StatusCode(500, e.Message);
            }
        }

        [HttpPost]
        [Route("Add")]
        public async Task<ActionResult> AddNewChatroom([FromBody] Topic topic)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            try
            {
                await TopicsService.AddTopic(topic);
                return Created($"/{topic.ID}", topic);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return StatusCode(500, e.Message);
            }
        }

        [HttpDelete]
        [Route("{id:string}")]
        public async Task<ActionResult> DeleteChatroom([FromRoute] Topic topic)
        {
            try
            {
                await TopicsService.RemoveTopic(topic);
                return Ok();
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return StatusCode(500, e.Message);
            }
        }
    }
}