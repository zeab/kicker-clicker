# kicker-clicker

DRIVER_LOCATION=C:\\Users\\pyros\\Desktop\\shoes\\geckodriver.exe

//run mysql
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD='' -d mysql:latest

//Users
SELECT * FROM `kicker`.`users`;
CREATE TABLE `kicker`.`users` (id VARCHAR(50), email VARCHAR(200), password VARCHAR(200), cv VARCHAR(5));
INSERT INTO `kicker`.`users`  (id, email, password, cv) VALUES ('', '', '', '');

//Drops
SELECT * FROM `kicker`.`drops`;
CREATE TABLE `kicker`.`drops` (id VARCHAR(50), name VARCHAR(200), color VARCHAR(200), url VARCHAR(250), dateTime VARCHAR(100), wanted VARCHAR(2), monitorPeriod VARCHAR(12));
INSERT INTO `kicker`.`drops`  (id, name, color, url, dateTime, wanted, monitorPeriod) VALUES ('', '', '', '', '', '', '');
DROP TABLE `kicker`.`drops`;


//run local firefox
docker run -d -p 4440:4444 --shm-size 2g selenium/standalone-firefox:3.141.59-20200525



CREATE TABLE `kicker`.`drops` (id VARCHAR(50), name VARCHAR(200), color VARCHAR(200), url VARCHAR(250), dateTime VARCHAR(100), wanted VARCHAR(2));





//run kicker clicker
docker run -d --name kickerclicker -p 7000:7000 -v /home/{user}/selenium:/selenium -e MYSQL_USER= -e MYSQL_PASSWORD= -e MYSQL_HOST= -e MYSQL_PORT= -e SELENIUM_REMOTE_DRIVER_HOST= -e SELENIUM_REMOTE_DRIVER_PORT= -e SELENIUM_SCREENSHOT_DIR= zeab/kickerclicker:latest

