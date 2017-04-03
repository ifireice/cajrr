VERSION := 2.0
RELEASE := $(shell git describe --always --tags | awk -F- '{ if ($$2) dot="."} END { printf "%s%s%s\n",$$2,dot,$$3}')
VENDOR := "SKB Kontur"
LICENSE := MIT
URL := https://github.com/skbkontur/cajrr

default: clean prepare test build packages

prepare:
	sudo apt-get -qq update
	sudo apt-get install -y rpm ruby-dev gcc make
	sudo gem install fpm

clean:
	@rm -rf build

prepare: clean

infra:
	docker-compose up -d

build:
	mvn package

test: clean prepare
	mvn test

up: clean build run

run: build
	/usr/bin/java -jar target/cajrr-$(VERSION)-SNAPSHOT.jar server pkg/config.yml
tar:
	mkdir -p build/root/usr/lib/cajrr
	mkdir -p build/root/usr/lib/systemd/system
	mkdir -p build/root/etc/cajrr

	mv target/cajrr-$(VERSION)-SNAPSHOT.jar build/root/usr/lib/cajrr/cajrr.jar
	cp etc/cajrr.service build/root/usr/lib/systemd/system/cajrr.service
	cp etc/config.yml build/root/etc/cajrr/config.yml

	tar -czvPf build/cajrr-$(VERSION).$(RELEASE).tar.gz -C build/root  .

rpm:
	fpm -t rpm \
		-s "tar" \
		--description "Cassandra Java Range Repair Service" \
		--vendor $(VENDOR) \
		--url $(URL) \
		--license $(LICENSE) \
		--name "cajrr" \
		--version $(VERSION) \
		--iteration "$(RELEASE)" \
		--after-install "./etc/postinst" \
		--config-files "/etc/cajrr/config.yml" \
		-p build \
		build/cajrr-$(VERSION).$(RELEASE).tar.gz

deb:
	fpm -t deb \
		-s "tar" \
		--description "Cassandra Java Range Repair Service" \
		--vendor $(VENDOR) \
		--url $(URL) \
		--license $(LICENSE) \
		--name "cajrr" \
		--version "$(VERSION)" \
		--iteration "$(RELEASE)" \
		--after-install "./etc/postinst" \
		--config-files "/etc/cajrr/config.yml" \
		-p build \
		build/cajrr-$(VERSION).$(RELEASE).tar.gz

packages: clean build tar rpm deb

.PHONY: test
