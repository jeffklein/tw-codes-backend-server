FROM fedora:21

RUN yum update -y && yum install -y maven

RUN mkdir /app

COPY . /app

WORKDIR /app

EXPOSE 8080

CMD ["./run.sh"]
