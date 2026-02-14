FROM bde2020/hadoop-base:2.0.0-hadoop3.2.1-java8

USER root

# Debian stretch is EOL -> use archive mirrors + disable Valid-Until checks
RUN set -eux; \
  sed -i 's|http://deb.debian.org/debian|http://archive.debian.org/debian|g' /etc/apt/sources.list; \
  sed -i 's|http://security.debian.org/debian-security|http://archive.debian.org/debian-security|g' /etc/apt/sources.list; \
  sed -i '/stretch-updates/d' /etc/apt/sources.list; \
  printf 'Acquire::Check-Valid-Until "false";\nAcquire::AllowInsecureRepositories "true";\n' > /etc/apt/apt.conf.d/99archive; \
  apt-get -o Acquire::Check-Valid-Until=false update; \
  apt-get install -y --no-install-recommends python3; \
  rm -rf /var/lib/apt/lists/*

# give a sane PATH by default
ENV PATH=/opt/hadoop-3.2.1/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
