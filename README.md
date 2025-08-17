# T2G2---COMP90018


## Run Bcakend

1.navigate to main project path

  `cd {your path} /plant-backend`

2.build docker image

  `docker build -t plant-world-backend:latest .`

3.run docker image

  `docker run -p 9999:9999 plant-world`

## Database
Excute sql file in main project path: plant_db.sql


## Test api

### Register
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
```

Replace with your own account.

<img width="968" height="491" alt="image" src="https://github.com/user-attachments/assets/f976a471-1883-4adc-a31f-6a341da33891" />

### Login
**POST url: **
  `http://{your address}:9999/user/login`

**POST body:**
```json
{
    "username": "pengyu3333",
    "password": "123456"
}
```

Replace with your own username and password.

**Response**: 

jwt token in field data: 

"eyJhbGciOiJIUzI1NiJ9.eyJ..."

**How to use token:** When making a new request, add the token to request header for authentication.
### Get UserInfo
**GET url: **
  `http://{your path}:9999/user/info`

Add jwt token to request headers:

Authorization: eyJhbGciOiJIUzI1NiJ9.ey...

<img width="2110" height="273" alt="image" src="https://github.com/user-attachments/assets/36c438eb-e680-40d8-9020-8007ce619d29" />
