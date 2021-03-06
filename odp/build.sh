#!/bin/bash
#author: fajun yang
#date: 5/2/2016
#description: odp-hisi build & install script
#$1: target output directory
#$2: target distributions name
#$3: target rootfs directory(absolutely)
#$4: kernel build directory(absolutely)
#return: 0: build success, other: failed

###################### Initialise variables ####################

echo "/packages/docker/build.sh: outputdir=$1, distro=$2, rootfs=$3, kernel=$4"

if [ "$1" = OUTPUT_DIR'' ] || [ "$2" = '' ] ||  [ "$3" = '' ]  || [ "$4" = '' ]; then
    echo "Invalid parameter passed. Usage ./docker/build.sh <outputdir> <distrib> <rootfs> <kernal>"
    exit
fi

echo "Installing odp package..."

OUTPUT_DIR=$1
DISTRO=$2
ROOTFS=$3
KERNEL_BUILD_DIR=$4
CROSS=$5  # such as aarch64-linux-gnu- on X86 platform or "" on ARM64 platform
PACK_TYPE=$6 # such as "tar", "rpm", "deb" or "all"
PACK_SAVE_DIR=$(cd $7; pwd) #
INSTALL_DIR=$8

ODP_ADDR=`pwd`/packages/odp

############################# build odp  #####################

#echo "init huge tables..."
#HUGE_PATH="$ROOTFS/mnt/huge/"
#if [ ! -d "$HUGE_PATH" ]; then
#sudo mkdir "$HUGE_PATH"
#fi
#sudo mount none $ROOTFS/mnt/huge -t hugetlbfs
#echo 400 > $ROOTFS/proc/sys/vm/nr_hugepages
#echo 0 > /proc/sys/kernel/randomize_va_space

LOCALARCH=`uname -m`
if [[ x"$LOCALARCH" =~ x"x86" ]]; then
	BUILDTYPE="cross"
	echo "arch is $LOCALARCH, build type is $BUILDTYPE"
else
	BUILDTYPE="native"
	echo "arch is $LOCALARCH, build type is $BUILDTYPE"
fi

echo "building the odp project..."

TARGETARCH="ARM64"
BUILDADDR=$ODP_ADDR/odp1.7/platform/linux-hisilicon/build

if [ x"BUILDTYPE" = x"cross" ]; then
	GNU_PREFIX=aarch64-linux-gnu-
	echo "when build type is cross, we copy the files to use directly..."
	# as the cross enviroment is not ok, we do so.
else
	GNU_PREFIX=
	echo "when build type is native, we copy the files to use directly..."
	# add the build scriptes to build odp project in later version
fi

WORKADDR="$ROOTFS/home/odp"
echo "create a odp workspace($WORKADDR)..."
if [ ! -d "$WORKADDR" ]; then
        sudo mkdir "$WORKADDR"
fi

echo "copy odp kernel driver and app to workspace..."
sudo cp $BUILDADDR/objs/ko/*.ko $WORKADDR/
sudo cp $BUILDADDR/objs/examples/* $WORKADDR/
echo "copy odp user driver to $ROOTFS/usr/lib/odp..."
if [ ! -d "$ROOTFS/usr/lib/odp" ]; then
	sudo mkdir "$ROOTFS/usr/lib/odp"
fi
sudo cp $BUILDADDR/objs/drv/*.so $ROOTFS/usr/lib/odp
echo "copy odp libs to $ROOTFS/usr/lib/..."
sudo cp $BUILDADDR/objs/lib/*.so $ROOTFS/usr/lib/

if [ x"${PACK_TYPE}" == x"tar" ] || [ x"${PACK_TYPE}" == x"all" ] ; then
    #Generate the corresponding files under the specifed directory
    pushd ${ODP_ADDR}
    sudo tar -czvf ${OUTPUT_DIR}/odp.tar.gz setup.sh remove.sh
    popd > /dev/null
    sudo cp ${OUTPUT_DIR}/odp.tar.gz ${PACK_SAVE_DIR}/
fi

echo "odp drive and app copy finished!"
echo "odp build finished!"

############################# Install odp finished #####################
