FROM alpine:3.7

COPY copy.sh /
RUN chmod +x copy.sh
CMD ["/copy.sh"]

COPY *.hpi /plugin/
COPY *.jpi /plugin/
