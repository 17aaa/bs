# 登录问题修复说明

## 问题原因
后端返回的数据格式是：
```json
{
  "status": "success",
  "data": {
    "userId": 4,
    "token": "eyJhbGci...",
    "walletConnected": false
  },
  "message": null
}
```

但前端代码检查的是 `response.data.token`，而实际上 token 在 `response.data.data.token` 里。

## 修复内容
修改了 `frontend/src/pages/LoginPage.jsx` 中的登录和注册逻辑：

### 登录逻辑修复前
```javascript
const response = await axios.post('http://localhost:8085/user/login', null, {
  params: { username, password },
});

if (response.data && response.data.token) {  // ❌ 错误
  // ...
}
```

### 登录逻辑修复后
```javascript
const response = await axios.post('http://localhost:8085/user/login', null, {
  params: { username, password },
});

// 后端返回格式：{status: "success", data: {token, userId, ...}}
if (response.data && response.data.status === 'success' && response.data.data) {
  const loginData = response.data.data;  // ✅ 正确
  localStorage.setItem('token', loginData.token);
  localStorage.setItem('userId', loginData.userId);
  // ...
}
```

### 注册逻辑同样修复

## 测试方法
1. 访问 http://localhost:3001
2. 进入登录页 http://localhost:3001/login
3. 使用测试账号登录：
   - 用户名：test
   - 密码：123456
4. 应该看到"登录成功！"提示并重定向到首页

## 后端 API 验证
```bash
# 测试登录 API
curl -X POST 'http://localhost:8085/user/login?username=test&password=123456'

# 应该返回：
# {"status":"success","data":{"userId":4,"walletConnected":false,"token":"..."}}
```