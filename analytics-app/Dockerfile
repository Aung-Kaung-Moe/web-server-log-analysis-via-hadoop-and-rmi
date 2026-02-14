FROM project-hadoop-client:latest

WORKDIR /app
COPY server.py /app/server.py
COPY index.html /app/index.html

EXPOSE 8000
CMD ["python3", "/app/server.py"]
