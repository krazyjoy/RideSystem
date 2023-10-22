//package com.rideSystem.Ride.controller;
//
//import com.rideSystem.Ride.stripe.ChargeRequest;
//import com.rideSystem.Ride.stripe.StripeService;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Charge;
//import lombok.extern.java.Log;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Service;
//import org.springframework.ui.Model;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.PostMapping;
//
//import org.springframework.web.bind.annotation.RestController;
//@Service
//@Log
//@Controller
//public class ChargeController {
//    @Autowired
//    StripeService paymentsService;
//
//    @PostMapping("/charge")
//    // public String charge(ChargeRequest chargeRequest, Model stripeModel) throws StripeException{
//    public String charge(ChargeRequest chargeRequest, Model stripeModel){
//        try{
//            log.info("inside charge");
//            chargeRequest.setDescription("Example charge");
//            chargeRequest.setCurrency(ChargeRequest.Currency.USD);
//            Charge charge = paymentsService.charge(chargeRequest);
//            stripeModel.addAttribute("id", charge.getId());
//            stripeModel.addAttribute("status",charge.getStatus());
//            stripeModel.addAttribute("chargeId",charge.getId());
//            stripeModel.addAttribute("balance_transaction",charge.getBalanceTransaction());
//            return "result";
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
//        return "result";
//    }
//
//    @ExceptionHandler(StripeException.class)
//    public String handleError(Model model, StripeException ex){
//        model.addAttribute("error", ex.getMessage());
//        return "result";
//    }
//
//}
package com.rideSystem.Ride.controller;
import com.rideSystem.Ride.stripe.ChargeRequest;
import com.rideSystem.Ride.stripe.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;

@Log
@Controller
public class ChargeController {

    @Autowired
    StripeService paymentsService;

    @PostMapping("/charge")
    public String charge(ChargeRequest chargeRequest, Model model) throws StripeException {
        log.info("inside /charge");
        chargeRequest.setDescription("Example charge");
        chargeRequest.setCurrency(ChargeRequest.Currency.EUR);
        Charge charge = paymentsService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());
        return "result";
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}