#!/usr/bin/env bash
host="localhost"
if [ "$1" != "" ]; then
host=$1
fi
end_points=()
end_points+=('buildinfo')
end_points+=('ping')
end_points+=('healthcheck')
for i in ${end_points[@]};
do
   printf "\n***** endpoint: /$i *****\n"
   curl http://$host:8000/$i
   printf "\n_____________________________________________________________\n"
done
