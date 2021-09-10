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

static float ethylene_level = 0.0;
static enum State current_state = UNRIPE;

static enum State simulate_sensor(){
	srand(time(NULL));
	double variation = (double)(rand() % 100) / 10;
	//there is a 1/50 probability that the ethylene level drops to 0 (fruits change)
	if (rand() % 50 != 0)
		ethylene_level += variation;
	else 
		ethylene_level = 0;
	

	if (ethylene_level < 250)
		return UNRIPE;
	if (ethylene_level < 400)
		return RIPE;
	return EXPIRED;
}

static void
get_ethylene_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	char message[30];
	char *data = NULL;

	//converting the level of ethylene in string
	//gcvt(ethylene_level, 5, message);
	sprintf(message, "%g", ethylene_level);
	data =	&message[0];
	printf("message: %s, length of the message: %d\n", message, strlen(message));
	printf("data: %s, length of the data: %d\n", data, strlen(data));
  
	coap_set_header_content_format(response, TEXT_PLAIN);
	coap_set_payload(response, data, strlen(data));
}


static void
ethylene_event_handler(void)
{
  enum State sensed_state = simulate_sensor();
  
  if (current_state != sensed_state){
	current_state = sensed_state;
	switch (current_state) {
		case UNRIPE:
			printf("Ethylene level: %f, fruits unripe\n", ethylene_level);
			break;
		case RIPE:
			printf("Ethylene level: %f, fruits ripe\n", ethylene_level);
			break;
		case EXPIRED:
			printf("Ethylene level: %f, fruits expired\n", ethylene_level);
			break;
	}
    //When the state change the ethylene level sensed is sent to the ripening notifier, that will change its led
    coap_notify_observers(&ethylene_sensor);
  }
}


