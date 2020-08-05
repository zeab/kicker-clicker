# kicker-clicker

DRIVER_LOCATION=C:\\Users\\pyros\\Desktop\\shoes\\geckodriver.exe

//run mysql
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD='' -d mysql:latest

//run local firefox
docker run -d -p 4444:4444 --shm-size 2g selenium/standalone-firefox:3.141.59-20200525



//run kicker clicker
docker run -d -v /home/siddhartha/selenium:/selenium --name kickerclicker -p 7000:7000 -e MYSQL_USER='' -e MYSQL_PASSWORD='' -e MYSQL_URL='' zeab/kickerclicker:latest
