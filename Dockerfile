FROM artifactory.devops.wepay-inc.com/wepay/java:3.1.2 

# Setup config location and add packages
RUN mkdir -p /jar/config 
ADD config/version.yml /jar/config/
ADD config/build-version.yml /jar/config/
RUN ln -s /jar/config/ /config &&\
  cp /opt/newrelic/newrelic.jar /config/newrelic.jar
ADD ./build/distributions/wehack.tar /jar/
ADD cli /jar/cli
WORKDIR /jar
EXPOSE 8000

# Run the service
RUN printf "%s\n" "bash /jar/cli/container/run.sh" >> /etc/init.d/wp_core
