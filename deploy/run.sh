#/bin/sh
nohup java -jar THUSearch.jar > backend.log &
cd frontend
nohup python manage.py runserver 0.0.0.0:5021 > front.log &
