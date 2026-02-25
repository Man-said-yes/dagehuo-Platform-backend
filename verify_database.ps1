# 设置MySQL连接信息
$mysqlUser = "root"
$mysqlPassword = "123456" # 请根据实际密码修改
$databaseName = "dagehuo"

# 验证字段是否存在
Write-Host "验证数据库字段..."
mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "SHOW COLUMNS FROM users;"

# 验证默认值
Write-Host "\n验证默认值设置..."
mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "INSERT INTO users(openid, nickname) VALUES('test_verify', '测试用户');"
mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "SELECT id, openid, nickname, credit_score FROM users WHERE openid = 'test_verify';"
mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "DELETE FROM users WHERE openid = 'test_verify';"
