﻿using System;
using System.Threading;
using System.Threading.Tasks;
using ChatClient.Models;
using Microsoft.AspNetCore.SignalR;
using Application.Models;

namespace WebApplication.SignalR
{
    public class ChatHub : Hub
    {
        
        public Task JoinChatRoom(string ChatRoomId)
        {
            Console.WriteLine($"User:{Context.ConnectionId} joined the chatroom:{ChatRoomId}");
            return Groups.AddToGroupAsync(Context.ConnectionId, ChatRoomId);
        }
        public Task LeaveChatRoom(string ChatRoomId)
        {
            return Groups.RemoveFromGroupAsync(Context.ConnectionId, ChatRoomId);
        }
        public Task SendMessage(Message message, string activeChatRoomId)
        {
            Console.WriteLine("message sent?");
            return Clients.Group(activeChatRoomId).SendAsync("ReceiveChatRoomMessage", message);
        }
        public Task SendMessageFragment(MessageFragment messageFragment, string activeChatRoomId)
        {
            Console.WriteLine("messageFragment sent?");
            return Clients.Group(activeChatRoomId).SendAsync("ReceiveChatRoomMessageFragment", messageFragment);
        }
        // public Task UpdateChatroom(Chatroom chatroom)
        // {
        //     Console.WriteLine("Chatroom update.");
        //     return Clients.Group(chatroom._id).SendAsync("ReceiveChatroomUpdate", chatroom);
        // }
    }
}