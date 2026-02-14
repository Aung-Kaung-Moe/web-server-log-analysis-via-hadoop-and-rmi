FROM alpine:3.20

RUN apk add --no-cache bash curl coreutils

WORKDIR /app
COPY uploader.sh /app/uploader.sh
RUN chmod +x /app/uploader.sh

CMD ["/app/uploader.sh"]
