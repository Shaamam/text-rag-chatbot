i:
	cd frontend && npm install

start:
	cd frontend && npm start

cb:
	./gradlew clean build

run:
	./gradlew bootRun