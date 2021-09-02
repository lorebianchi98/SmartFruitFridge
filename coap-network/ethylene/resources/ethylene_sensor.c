#include <stdio.h>
#include <stdlib.h>
#include "coap-engine.h"
#include <time.h>

static void get_ethylene_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void ethylene_event_handler(void);

enum State{UNRIPE, RIPE, EXPIRED};

EVENT_RESOURCE(ethylene_sensor,
         "title=\"Ethylene sensor\";obs",
         get_ethylene_handler,
         NULL,
         NULL,
         NULL,
         ethylene_event_handler);

static double ethylene_level = 10.0;
static enum State current_state = UNRIPE;

static enum State simulate_sensor(){
	srand(time(NULL));
	double variation = (double)(rand() % 100) / 10;
	//there is a 1/20 probability that the ethylene level decrease
	if (rand() % 20 != 0)
		ethylene_level += variation;
	else {
		ethylene_level -= variation * 20;
		if (ethylene_level < 0)
			ethylene_level = 0;
	}
	
	if (ethylene_level < 100)
		return UNRIPE;
	if (ethylene_level < 200)
		return RIPE;
	return EXPIRED;
}

static void
get_ethylene_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	const char* message = NULL;
  
	printf("Ho ricevuto una get");

	switch (current_state) {
		case UNRIPE:
			message = "unripe";
			break;
		case RIPE:
			message = "ripe";
			break;
		case EXPIRED:
			message = "expired";
			break;
	}
  
	coap_set_header_content_format(response, TEXT_PLAIN);
	coap_set_payload(response, message, strlen(message));
}


static void
ethylene_event_handler(void)
{
  enum State sensed_state = simulate_sensor();
  
  if (current_state != sensed_state){
	current_state = sensed_state;
	switch (current_state) {
		case UNRIPE:
			printf("Fruits unripe\n");
			break;
		case RIPE:
			printf("Fruits ripe\n");
			break;
		case EXPIRED:
			printf("Fruits expired\n");
			break;
	}
    	
    coap_notify_observers(&ethylene_sensor);
  }
}


