#
# Copyright (c) kengo92i All rights reserved.
#
# How to use simulator for ChainVoxel.
# Type as below on Terminal.app.
#     make
#    make test
# or
#    make
#    make test SITES=5 OPERATIONS=10 LIMIT=3
#

CURDIR	= Simulator
SRCDIR	= src
OBJDIR	= bin
JAVADOCDIR	= javadoc
OBJS		= \
	${OBJDIR}/Voxel.class \
	${OBJDIR}/ChainVoxel.class \
	${OBJDIR}/CRDT.class \
	${OBJDIR}/GroupEntry.class \
	${OBJDIR}/OperationQueue.class \
	${OBJDIR}/Operation.class \
	${OBJDIR}/Simulator.class \
	${OBJDIR}/StructureTable.class \
	${OBJDIR}/Site.class 
SOURCES		= ${OBJS:${OBJDIR}/%.class=${SRCDIR}/%.java}
COMPILER	= javac
JAVA 		= java -ea
JFLAGS		= -encoding utf-8 -d ${OBJDIR}
TARGET		= Simulator
SITES		= 10
OPERATIONS	= 100
LIMIT		= 1

all:
	if [ ! -e ${OBJDIR} ]; then mkdir ${OBJDIR}; fi
	cp -r ${SRCDIR}/xml ${OBJDIR}
	${COMPILER} ${JFLAGS} ${SOURCES}
test:
	cd ${OBJDIR}; ${JAVA} ${TARGET} ${SITES} ${OPERATIONS} ${LIMIT}
clean:
	${RM} -r ${OBJDIR} ${JAVADOCDIR}
tar: clean
	cd ..; tar cvzf ${CURDIR}.tgz ${CURDIR}     
javadoc:
	javadoc -encoding UTF-8 -charset UTF-8 -d ${JAVADOCDIR} -private ${SOURCES}
tree:
	find .
