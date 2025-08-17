# T2G2---COMP90018


## Run Bcakend

1.navigate to main project path

  `cd {your path} /plant-backend`

2.build docker image

  `docker build -t plant-world-backend:latest .`

3.run docker image

  `docker run -p 9999:9999 plant-world`

## Database



## Test Register api
POST url: 
  `http://{your address}:9999/user/reg`

POST body:
```json
{
  "username": "pengyu3333",
  "nickname": "dasdsa",
  "phone": "13812345678",
  "password": "123456",
  "email": "pengyu3@example.com",
  "wechat": "pengyu3_wechat",
  "userType": "normal",
  "avatar": "default.png"
}

Replace with your own account.
