package br.com.jonyfs.credit.card.api.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.jonyfs.credit.card.api.exceptions.EntityNotFoundException;
import br.com.jonyfs.credit.card.api.exceptions.InvalidRequestException;
import br.com.jonyfs.credit.card.api.model.Payment;
import br.com.jonyfs.credit.card.api.resource.PaymentResource;
import br.com.jonyfs.credit.card.api.resource.PaymentResourceAssemblerV2;
import br.com.jonyfs.credit.card.api.service.PaymentService;
import br.com.jonyfs.credit.card.api.util.ResourcePaths;

@RestController
@ExposesResourceFor(Payment.class)
@RequestMapping(
                value = ResourcePaths.Payment.V2.ROOT)
public class PaymentControllerV2 {

    @Resource
    PaymentService             paymentService;

    @Resource
    PaymentResourceAssemblerV2 paymentResourceAssembler;

    @ResponseBody
    @RequestMapping(
                    method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<org.springframework.hateoas.Resource<String>> doPayment(@RequestBody @Valid Payment payment, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException("Invalid " + payment.getClass().getSimpleName(), bindingResult);
        }
        payment = paymentService.doPayment(payment);

        org.springframework.hateoas.Resource<String> resource = new org.springframework.hateoas.Resource<String>(payment.getId());
        resource.add(paymentResourceAssembler.linkToSingleResource(payment));

        return new ResponseEntity<org.springframework.hateoas.Resource<String>>(resource, HttpStatus.CREATED);
    }

    @ResponseBody
    @RequestMapping(
                    value = ResourcePaths.ID, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<PaymentResource> getPayment(@PathVariable(
                    value = "id") String id) {
        Payment entity = paymentService.getPayment(id);
        if (entity == null) {
            throw new EntityNotFoundException(String.valueOf(id));
        }
        final PaymentResource resource = paymentResourceAssembler.toResource(entity);
        return ResponseEntity.ok(resource);
    }

    @RequestMapping(
                    method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public PagedResources<PaymentResource> query(Pageable pageable, PagedResourcesAssembler<Payment> assembler) {
        Page<Payment> payments = paymentService.findAll(pageable);
        return assembler.toResource(payments, paymentResourceAssembler);
    }

}
