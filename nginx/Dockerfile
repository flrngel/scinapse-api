FROM nginx

RUN rm -rf /etc/nginx/conf.d/default.conf

COPY conf/absolute.conf  /etc/nginx/conf.d/absolute.conf
COPY conf/nginx.conf  /etc/nginx/nginx.conf

VOLUME ["/data", "/etc/nginx", "/var/log/nginx"]

WORKDIR /etc/nginx

CMD ["nginx"]

EXPOSE 80
EXPOSE 443