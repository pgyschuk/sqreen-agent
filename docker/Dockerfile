FROM tomcat:9.0.30

COPY agent-jar-with-dependencies.jar agent.jar
ENV CATALINA_OPTS="$CATALINA_OPTS -javaagent:\"agent.jar\""

CMD ["catalina.sh", "run"]
