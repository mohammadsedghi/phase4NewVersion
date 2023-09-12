package com.example.finalproject_phase2.controller;

import com.example.finalproject_phase2.custom_exception.CustomException;
import com.example.finalproject_phase2.custom_exception.CustomInputOutputException;
import com.example.finalproject_phase2.custom_exception.CustomNoResultException;
import com.example.finalproject_phase2.dto.customerDto.CustomerDtoEmail;
import com.example.finalproject_phase2.dto.ordersDto.OrdersDto;
import com.example.finalproject_phase2.dto.specialistDto.*;
import com.example.finalproject_phase2.dto.specialistSuggestionDto.StatusOrderSpecialistSuggestionDtoWithOrderAndSpecialist;
import com.example.finalproject_phase2.dto.specialistSuggestionDto.SuggestionWithSpecialistAndOrdersDto;
import com.example.finalproject_phase2.dto.specialistSuggestionDto.ValidSpecialistSuggestionDto;
import com.example.finalproject_phase2.dto.subDutyDto.SubDutyNameDto;
import com.example.finalproject_phase2.service.email.EmailRequest;
import com.example.finalproject_phase2.entity.Orders;
import com.example.finalproject_phase2.entity.Specialist;
import com.example.finalproject_phase2.entity.enumeration.SpecialistSelectionOfOrder;
import com.example.finalproject_phase2.securityConfig.AuthenticationResponse;
import com.example.finalproject_phase2.service.*;
import com.example.finalproject_phase2.mapper.OrdersMapper;
import com.example.finalproject_phase2.mapper.SpecialistMapper;
import com.example.finalproject_phase2.service.email.MailService;
import com.example.finalproject_phase2.util.CheckValidation;
import com.example.finalproject_phase2.util.validation.DtoValidation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/specialist")
public class SpecialistController {
    private final SpecialistService specialistService;
    private final OrdersService ordersService;
    private final CustomerCommentsService customerCommentsService;
    private final SpecialistSuggestionService specialistSuggestionService;
    private final OrdersMapper ordersMapper;
    private final SpecialistMapper specialistMapper;
    private final MailService mailService;
    private final WalletService walletService;
    DtoValidation dtoValidation=new DtoValidation();

    @Autowired
    public SpecialistController(SpecialistService specialistService, CustomerCommentsService customerCommentsService, OrdersService ordersService, SpecialistSuggestionService specialistSuggestionService, OrdersMapper ordersMapper, SpecialistMapper specialistMapper, MailService mailService, WalletService walletService) {
        this.specialistService = specialistService;
        this.customerCommentsService = customerCommentsService;
        this.ordersService = ordersService;
        this.specialistSuggestionService = specialistSuggestionService;
        this.ordersMapper = ordersMapper;
        this.specialistMapper = specialistMapper;
        this.mailService = mailService;
        this.walletService = walletService;
    }
    @PostMapping(value = "/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dutyName") String dutyName,
            @RequestParam("subDutyName") String subDutyName,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("nationalId") String nationalId,
            @RequestParam("email") String email,
            @RequestParam("password") String password)

          {
        System.out.println(file.getSize());
        SpecialistRegisterDto specialistRegisterDto=new SpecialistRegisterDto(firstName,lastName,nationalId,email,password,dutyName,subDutyName);
        return  ResponseEntity.ok(specialistService.register(file,specialistRegisterDto));
    }

//    @PostMapping(value = "/register")
//    public ResponseEntity<AuthenticationResponse> register(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("a") Integer a,
//            @RequestParam("specialistDto")@Valid SpecialistDto specialistDto){
//        System.out.println(a);
//        System.out.println(specialistDto.getLastName());
//        System.out.println(file.getSize());
//        return  ResponseEntity.ok(specialistService.register(file,specialistDto));
//    }
    @PostMapping("/authentication")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody SpecialistLoginDto specialistLoginDto
            , @RequestParam String userType){
        CheckValidation.userType=userType;
        System.out.println(userType);
        if (userType.equals("specialist")){
            return  ResponseEntity.ok(specialistService.authenticate(specialistLoginDto));
        }else  return new ResponseEntity<>(new AuthenticationResponse(), HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/updateScore")
    public ResponseEntity<Integer> updateScore(@RequestBody @Valid SpecialistScoreDto specialistScoreDto) {
        Integer score  = specialistService.updateSpecialistScore(specialistScoreDto);
        if (score!=null)return new ResponseEntity<>(score, HttpStatus.ACCEPTED);
        else throw new CustomException("score has not been updated");
    }
    @PostMapping("/showImage")
    public ResponseEntity<String> showImage(@RequestBody @Valid ConvertImageDto convertImageDto) {
        try {
            specialistService.convertByteArrayToImage(convertImageDto);
            return new ResponseEntity<>("image is converted", HttpStatus.ACCEPTED);
        } catch (CustomException ce) {
            throw new CustomException(ce.getMessage());
        }
    }
    //src/main/java/com/example/finalproject_phase2/util/images/300.jpg
    @PostMapping("/setImage")
    public ResponseEntity<String> setImage(@RequestBody @Valid SpecialistImageDto specialistImageDto) {
        try {
            specialistService.convertImageToImageData(specialistImageDto);
            return new ResponseEntity<>("image is converted", HttpStatus.ACCEPTED);
        } catch (CustomInputOutputException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/showOrdersToSpecialist")
    public ResponseEntity<Collection<OrdersDto>> showOrdersToSpecialist(@RequestBody @Valid SubDutyNameDto subDutyNameDto ) {
        Collection<Orders> ordersCollection = ordersService.showOrdersToSpecialist(subDutyNameDto);
        Collection<OrdersDto> ordersDtoCollection = ordersMapper.collectionOrdersToCollectionOrdersDto(ordersCollection);
        return new ResponseEntity<>(ordersDtoCollection, HttpStatus.ACCEPTED);
    }
    @PostMapping("/showScore")
    public ResponseEntity<Integer> showScore(@RequestBody @Valid SpecialistDto specialistDto ) {
        Integer score = customerCommentsService.showScoreOfLastCustomerCommentsThatThisSpecialistIsExist(
                specialistMapper.specialistDtoToSpecialist(specialistDto));
        return new ResponseEntity<>(score, HttpStatus.ACCEPTED);
    }
    @PostMapping("/submitSpecialSuggestion")
    public ResponseEntity<Boolean> IsValidSpecialSuggestion(@RequestBody @Valid ValidSpecialistSuggestionDto validSpecialistSuggestionDto) {
        specialistSuggestionService.IsValidSpecialSuggestion(validSpecialistSuggestionDto);
        return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
    }
    @PostMapping("/findSuggestWithThisSpecialistAndOrder")
    public ResponseEntity<Boolean> findSuggestWithThisSpecialistAndOrder(@RequestBody @Valid StatusOrderSpecialistSuggestionDtoWithOrderAndSpecialist statusOrderSpecialistSuggestionDtoWithOrderAndSpecialist ) {
        specialistSuggestionService.findSuggestWithThisSpecialistAndOrder(statusOrderSpecialistSuggestionDtoWithOrderAndSpecialist);
        return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
    }
    @PostMapping("/changeSpecialistSelectedOfOrder")
    public ResponseEntity<SpecialistSelectionOfOrder> changeSpecialistSelectedOfOrder(@RequestBody @Valid SpecialistSelectionOfOrder specialistSelectionOfOrder  ) {
        SpecialistSelectionOfOrder specialistSelectionOfOrderCandidate = specialistSuggestionService.changeSpecialistSelectedOfOrder(specialistSelectionOfOrder);
        return new ResponseEntity<>(specialistSelectionOfOrderCandidate, HttpStatus.ACCEPTED);
    }

    @PostMapping("/changeStatusOrderToWaitingForSpecialistToWorkplace")
    public ResponseEntity<Boolean> changeStatusOrderToWaitingForSpecialistToWorkplace(@RequestBody  SuggestionWithSpecialistAndOrdersDto specialistAndOrdersDto ) {
        dtoValidation.isValid(specialistAndOrdersDto);
        Optional<Orders> order = ordersService.findById(specialistAndOrdersDto.getOrderId());
        Specialist specialist = specialistService.findByEmail(specialistAndOrdersDto.getSpecialistEmail());
        if (order.isPresent()) {
            specialistSuggestionService.changeStatusOrderToWaitingForSpecialistToWorkplace(order.get(),specialist);
        }else throw new CustomException("order not found with this id");
        return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
    }
    @PostMapping("/changePassword")
    public ResponseEntity<Boolean> changePassword(@RequestBody   SpecialistChangePasswordDto specialistChangePasswordDto) {
       dtoValidation.isValid(specialistChangePasswordDto);
        if (specialistService.changePassword(specialistChangePasswordDto.getEmail(), specialistChangePasswordDto.getNewPassword())) {
            return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        } else throw new CustomNoResultException("password not changed");

    }

    @PostMapping("/email/send")
    public ResponseEntity <Map<String,String>> sendEmail(@RequestBody EmailRequest emailRequest) {
        mailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getText());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email sent successfully!");

        return ResponseEntity.ok(response);
    }
    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") String token,Model model) {
        String response;
        System.out.println("controller token" + token);
        if (CheckValidation.memberTypespecialist.getIsEnable()){
            response="you clicked before and can not permission to clicked it again";
            model.addAttribute("response",response);
            return "activationFailure"; // Redirect to a failure page
        }
        if (specialistService.isAccountActivated(token)) {
            response="please wait that admin confirm you";
            model.addAttribute("response",response);
            return "activationSuccess"; // Redirect to a success page
        } else {
            response="some thing is wrong please try again";
            model.addAttribute("response",response);
            return "activationFailure"; // Redirect to a failure page
        }
    }
    @PostMapping("/wallet/ShowBalance")
    public ResponseEntity<Double> ShowBalance(@RequestBody  CustomerDtoEmail customerDtoEmail){
      dtoValidation.isValid(customerDtoEmail);
        Specialist specialist = specialistService.findByEmail(customerDtoEmail.getEmail());
        return new ResponseEntity<>(walletService.ShowBalance(specialist.getWallet()),HttpStatus.ACCEPTED);
    }



}
