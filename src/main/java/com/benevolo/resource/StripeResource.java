package com.benevolo.resource;

import com.benevolo.util.StripeLogic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;



@Produces(MediaType.APPLICATION_JSON)
@Path("/payment")
public class StripeResource {
   final private String STRIPE_SECRET;
    @Inject
    public StripeResource(@ConfigProperty(name="STRIPE_API_KEY") String stripeSecret) {
        STRIPE_SECRET = stripeSecret;
    }

    @POST
    @Path("/payment-intent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPaymentIntent(String payload) {
            StripeLogic logic = new StripeLogic(payload);
            return logic.createPaymentIntent();


    }
    @POST
    @Path("/refund")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRefund(String payload) throws JsonProcessingException, StripeException {
        try {
            Stripe.apiKey = STRIPE_SECRET;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode requestData = mapper.readTree(payload);
            long amount = requestData.get("amount").asLong();
            String paymentIntentId = requestData.get("paymentIntent").asText();
            RefundCreateParams params = RefundCreateParams.builder().setPaymentIntent(paymentIntentId).setAmount(amount).build();
            Refund refund = Refund.create(params);
            return Response.noContent().build();
        }catch(StripeException e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Fehler beim erstellen der Rueckerstattung").build();
        }
    }
}