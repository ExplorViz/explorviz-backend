sed -i "s#mongo.host=.*#mongo.host=$MONGO_HOST#g" META-INF/explorviz-custom.properties

sed -i "s#mongo.port=.*#mongo.port=$MONGO_PORT#g" META-INF/explorviz-custom.properties

sed -i "s#redis.host=.*#redis.host=$REDIS_HOST#g" META-INF/explorviz-custom.properties

sed -i "s#service.prefix=.*#service.prefix=$SERVICE_PREFIX#g" META-INF/explorviz-custom.properties

sed -i "s#services.settings=.*#services.settings=$SERVICES_SETTINGS#g" META-INF/explorviz-custom.properties