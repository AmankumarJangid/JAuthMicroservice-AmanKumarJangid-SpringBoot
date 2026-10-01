# TODO's

- [✔️] Add Otp Rate Limit with Redis which uses cooldownkey to limit the number of OTP requests per user per time frame.
- [✔️] Add Otp Expiration with Redis which uses expiring key to set a time limit for OTP validity.
- [ ] Create api rate limit for the entire application which uses a combination of user id and ip address to limit the number of requests per user per time frame.