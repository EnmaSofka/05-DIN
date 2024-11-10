package com.bank.management;

import com.bank.management.data.AllCustomerDTO;
import com.bank.management.data.CustomerDTO;
import com.bank.management.data.RequestCreateCustomerDTO;
import com.bank.management.data.RequestGetCustomerDTO;
import com.bank.management.exception.CustomerAlreadyExistsException;
import com.bank.management.exception.CustomerNotFoundException;
import com.bank.management.usecase.CreateCustomerUseCase;
import com.bank.management.usecase.DeleteCustomerUseCase;
import com.bank.management.usecase.GetAllCustomersUseCase;
import com.bank.management.usecase.GetCustomerByIdUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final GetAllCustomersUseCase getAllCustomersUseCase;
    private final GetCustomerByIdUseCase getCustomerByidUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase, DeleteCustomerUseCase deleteCustomerUseCase, GetAllCustomersUseCase getAllCustomersUseCase, GetCustomerByIdUseCase getCustomerByidUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
        this.getAllCustomersUseCase = getAllCustomersUseCase;
        this.getCustomerByidUseCase = getCustomerByidUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseMs<Map<String, String>>> createCustomer(@RequestBody RequestMs<RequestCreateCustomerDTO> request) {

        try {
            request.validateDinHeaderFields();

            Customer customerDomain = new Customer.Builder()
                    .username(request.getDinBody().getUsername())
                    .build();

            Optional<Customer> customerCreated = createCustomerUseCase.apply(customerDomain);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("username", customerCreated.map(Customer::getUsername).orElse(""));

            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    responseData,
                    DinErrorCode.SUCCESS,
                    HttpStatus.CREATED,
                    "Customer creation process completed."
            );

        } catch (CustomerAlreadyExistsException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.OPERATION_FAILED,
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );

        } catch(IllegalArgumentException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    ""
            );
        } catch (Exception e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.OPERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseMs<Map<String, String>>> deleteCustomer(@RequestBody RequestMs<RequestGetCustomerDTO> request) {

        try {
            request.validateDinHeaderFields();
            boolean isDeleted = deleteCustomerUseCase.apply(request.getDinBody().getId());

            Map<String, String> responseData = new HashMap<>();
            responseData.put("id", String.valueOf(request.getDinBody().getId()));

            if (isDeleted) {
                return ResponseBuilder.buildResponse(
                        request.getDinHeader(),
                        responseData,
                        DinErrorCode.CUSTOMER_DELETED,
                        HttpStatus.OK,
                        "Customer deleted successfully."
                );
            } else {
                return ResponseBuilder.buildResponse(
                        request.getDinHeader(),
                        responseData,
                        DinErrorCode.OPERATION_FAILED,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error deleting customer."
                );
            }

        } catch (CustomerNotFoundException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.CUSTOMER_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );

        } catch(IllegalArgumentException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    ""
            );
        } catch (Exception e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.OPERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }



    @PostMapping
    public ResponseEntity<ResponseMs<List<AllCustomerDTO>>> getAllCustomers(@RequestBody RequestMs<Void>  request) {

        try {
            request.validateDinHeaderFields();
            List<Customer> customers = getAllCustomersUseCase.apply();

            List<AllCustomerDTO> AllcustomerDTOs = customers.stream()
                    .map(customer -> new AllCustomerDTO.Builder()
                            .setUsername(customer.getUsername())
                            .setId(customer.getId())
                            .build())
                    .toList();

            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    AllcustomerDTOs,
                    DinErrorCode.SUCCESS,
                    HttpStatus.OK,
                    "All customers retrieved successfully."
            );
        } catch(IllegalArgumentException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    ""
            );
        } catch (Exception e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.OPERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while retrieving all customers."
            );
        }
    }



    @PostMapping("/get")
    public ResponseEntity<ResponseMs<CustomerDTO>> getCustomerById(@RequestBody RequestMs<RequestGetCustomerDTO> request) {

        try {
            request.validateDinHeaderFields();
            Customer customer = getCustomerByidUseCase.apply(request.getDinBody().getId());

            CustomerDTO customerDTO = new CustomerDTO.Builder()
                    .setUsername(customer.getUsername())
                    .build();

            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    customerDTO,
                    DinErrorCode.SUCCESS,
                    HttpStatus.OK,
                    "Customer retrieved successfully."
            );

        } catch (CustomerNotFoundException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.CUSTOMER_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );
        } catch(IllegalArgumentException e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.BAD_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    ""
            );
        } catch (Exception e) {
            return ResponseBuilder.buildResponse(
                    request.getDinHeader(),
                    null,
                    DinErrorCode.OPERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage()
            );
        }
    }

}
