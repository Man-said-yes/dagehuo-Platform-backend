# 设置MySQL连接信息
$mysqlUser = "root"
$mysqlPassword = "123456"
$databaseName = "dagehuo"

# 检查字段是否存在
$columnExists = mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "SHOW COLUMNS FROM meal_events LIKE 'type';" | Select-String -Pattern "type"

if (-not $columnExists) {
    # 字段不存在，添加字段
    mysql -u $mysqlUser --password=$mysqlPassword $databaseName -e "ALTER TABLE meal_events ADD COLUMN type TINYINT DEFAULT 0 COMMENT '活动类型：0其他，1运动，2约饭，3学习，4游戏，5出行';"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "数据库字段添加成功！"
    } else {
        Write-Host "数据库字段添加失败！"
    }
} else {
    Write-Host "字段已存在，无需添加！"
}