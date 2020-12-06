﻿using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Application.Models;
using Application.SCMediator;

namespace Application.Services
{
    public class AccountsServiceImpl : IAccountService
    {
        public List<Account> Accounts { get; set; }
        public Model model;
        private bool wait=false;

        public AccountsServiceImpl(Model modelManager)
        {
            this.model = modelManager;
            
        }

        public async Task Register(Account account)
        {
            this.Accounts.Add(account);
            await model.Register(account);
        }

        public Task Register(string account)
        {
            throw new NotImplementedException();
        }

        public async Task<Account> LogIn(string username, string password)
        {
            Account account = null;
            foreach (var VARIABLE in Accounts)
            {
                if (VARIABLE.Pass.Equals(password) & VARIABLE.Username.Equals(username))
                {
                    account = VARIABLE;
                }
            }

            return account;
        }

        public async Task RemoveAccount(string accountID)
        {
            foreach (var VARIABLE in Accounts)
            {
                if (VARIABLE._id.Equals(accountID))
                {
                    Accounts.Remove(VARIABLE);
                }
            }

            await model.RemoveUser(accountID);
        }
        
        public async Task RequestAccounts()
        {
             await model.RequestUsers();
        }

        public async Task<IList<Account>> GetAllAccounts()
        {
            await RequestAccounts();
            while (wait == false)
            {
                Task.Delay(25);
                
            }
            return Accounts;
        }
        public async Task SetListOfAccounts(List<Account> accounts)
        { 
            this.Accounts = accounts;
            wait = true;

        }
    }
}