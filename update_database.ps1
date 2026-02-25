# 设置MySQL连接信息
$mysqlUser = "root"
$mysqlPassword = "123456" # 请根据实际密码修改
$databaseName = "dagehuo"

# 检查字段是否存在
$columnExists = mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "SHOW COLUMNS FROM users LIKE 'credit_score';" | Select-String -Pattern "credit_score"

if (-not $columnExists) {
    # 字段不存在，添加字段
    mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "ALTER TABLE users ADD COLUMN credit_score INT DEFAULT 100 COMMENT '信誉分：默认100分';"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "数据库字段添加成功！"
    } else {
        Write-Host "数据库字段添加失败！"
    }
} else {
    Write-Host "字段已存在，无需添加！"
}