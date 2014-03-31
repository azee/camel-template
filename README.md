camel-template
==============

A template for REST + Camel + ActiveMQ srvice

A simple example how Camel and Hazelcast can be used in distributive systems.

All server nodes are synchronised using Hazelcast.
In Hazelcast we store Camel aggregators state.
As a JMS implementation ActiveMQ is used. It has to be installed and configured.
However ActiveMQ is not needed during build and testing process.

Service
==============
This template implements a TV voting service.

We have a Poll with Competitors. We have a REST-API to create,
stop Polls and to send Votes (e.g. via mobile application or another SMS-parsing application).

All calculations are processed in Poll Camel Aggregator and are synchronised via Hazelcast
between all servers in the list (hazelcast.members).

We don't use any database to share the state. All transactions are synchronised within aggregator.

After Poll is stopped we can perform post processing (e.g. save it into persistant database).

Stability
==============
Thanks to Hazelcast we won't loose the state even if all servers (except one of the group) will crash.

We won't lose a single vote during high load peaks.
As we define maximum number of parallel consumers all unprocessed messages will be kept in ActiveMQ queues
and will be processed when consumers are free.

REST can process high RPS because the only thing it does - it puts messages into JMS. It doesn't have
to communicate with any database to store data.

Scalability
==============
In the system like this following JMS configuration is preferred: each server has it's own ActiveMQ.

In this case we can leave JMS + Camel processors in one cluster and REST in another.
We can easily add as much servers as we need to any of them.

Deploy
==============
REST component can be deployed to any application server.

Camel component can be started like this:

java -cp "/usr/share/PATH_TO_TEMPLATE/lib/*" -Xmx8192m -Xloggc:/var/log/PATH_TO_GC_LOG.log -Xbootclasspath/a:/etc/PATH_TO_CONFIG_FILES -jar /usr/share/PATH_TO_TEMPLATE/template-camel.jar > /dev/null 2>&1 &

Also Camel context can be included into the REST Web context. In that case REST and Camel components will work on the same server in the common context.F

Jenkins Build
==============
http://azee.people.yandex.net/job/template-camel/
