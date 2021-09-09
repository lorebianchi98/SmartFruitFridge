#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"


#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static void put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(ripening_notifier,
         "title=\"Ripening notifier\";rt=\"Control\"",
         NULL,
         NULL,
         put_handler,
         NULL);

enum State{UNRIPE, RIPE, EXPIRED};
enum State current_state = UNRIPE;
float ethylene_level = 0.0;

static void
put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	size_t len = 0;
  const uint8_t* payload = NULL;
  int success = 1;
	
  if((len = coap_get_payload(request, &payload))) {
		//adapting the color of the led to the state of the fruit
		ethylene_level = atof((char*)payload);
		if(ethylene_level < 250){ 
			current_state = UNRIPE;
			LOG_INFO("Fruits unripe\n");
			leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
		}
		else if(ethylene_level < 400){ 
			current_state = RIPE;
			LOG_INFO("Fruits ripe\n");
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		}else{
			current_state = EXPIRED;
			LOG_INFO("Fruits expired\n");
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		}

	}  

  if(!success) 
    coap_set_status_code(response, BAD_REQUEST_4_00);  
}


