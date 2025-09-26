i:
	cd frontend && npm install

start:
	cd frontend && npm start

cb:
	./gradlew clean build

run:
	./gradlew bootRun

kill:
	lsof -i :8080 | grep LISTEN | awk '{print $$2}' | xargs kill -9

test:
	cd /Users/shaama/Documents/openai-mcp-demo/textrag && ./gradlew test --tests "io.shaama.textrag.integration.VectorStoreIntegrationTest" --info