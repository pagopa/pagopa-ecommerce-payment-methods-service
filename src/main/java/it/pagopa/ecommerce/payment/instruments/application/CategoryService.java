package it.pagopa.ecommerce.payment.instruments.application;

import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentCategory;
import it.pagopa.ecommerce.payment.instruments.domain.aggregates.PaymentInstrumentCategoryFactory;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryID;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentCategoryName;
import it.pagopa.ecommerce.payment.instruments.domain.valueobjects.PaymentInstrumentType;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryDocument;
import it.pagopa.ecommerce.payment.instruments.infrastructure.PaymentInstrumentCategoryRepository;
import it.pagopa.ecommerce.payment.instruments.utils.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static it.pagopa.ecommerce.payment.instruments.exception.CategoryAlreadyInUseException.categoryAlreadyInUseException;
import static it.pagopa.ecommerce.payment.instruments.exception.CategoryNotFoundException.categoryNotFoundException;

@Service
@ApplicationService
@Slf4j
public class CategoryService {
    @Autowired
    private PaymentInstrumentCategoryRepository categoryRepository;

    @Autowired
    private PaymentInstrumentCategoryFactory categoryFactory;

    public Flux<PaymentInstrumentCategory> getCategories(){
        return categoryRepository.findAll().map(
                this::convertDocToAggregate
        );
    }

    public Mono<PaymentInstrumentCategory> getCategory(String categoryId){
        return categoryRepository.findById(categoryId).map(
                this::convertDocToAggregate
        );
    }

    public Mono<PaymentInstrumentCategory> getCategoryByName(String name){
        return categoryRepository.findByPaymentInstrumentCategoryName(name).map(
                this::convertDocToAggregate
        );
    }

    public Mono<PaymentInstrumentCategory> createCategory(String name, List<String> types){
        log.debug("[Payment instrument Category Aggregate] Create new Aggregate");
        Mono<PaymentInstrumentCategory>  paymentInstrumentCategoryMono = categoryFactory.newCategory(
                new PaymentInstrumentCategoryID(UUID.randomUUID()),
                new PaymentInstrumentCategoryName(name),
                types.stream().map(PaymentInstrumentType::new).toList()
        );

        log.debug("[Payment instrument Aggregate] Store Aggregate");
        return paymentInstrumentCategoryMono.flatMap( c -> categoryRepository.save(
                new PaymentInstrumentCategoryDocument(
                        c.getPaymentInstrumentCategoryID().value().toString(),
                        c.getPaymentInstrumentCategoryName().value(),
                        c.getPaymentInstrumentTypes().stream().map(
                                PaymentInstrumentType::value
                        ).collect(Collectors.toList())
                ))
                .map(this::convertDocToAggregate)
        );
    }

    public Mono<PaymentInstrumentCategory> updateCategory(String id, String name, List<String> types) {
        log.info("[Payment instrument Category Aggregate] Update Aggregate");

        return categoryRepository.findById(id).hasElement().flatMap(
                exist -> {
                    if(Boolean.TRUE.equals(exist)){
                        return categoryRepository.findByPaymentInstrumentCategoryName(name).flatMap(
                                duplicated -> {
                                    if(duplicated.getPaymentInstrumentCategoryID().equals(id)){

                                        return categoryRepository
                                                .save(new PaymentInstrumentCategoryDocument(id, name, types))
                                                .map(this::convertDocToAggregate);
                                    } else { // update name -> conflict
                                        throw categoryAlreadyInUseException(new PaymentInstrumentCategoryName(name));
                                    }
                                }
                        ).switchIfEmpty(categoryRepository
                                .save(new PaymentInstrumentCategoryDocument(id, name, types))
                                .map(this::convertDocToAggregate)
                        );
                    } else {
                        throw categoryNotFoundException(new PaymentInstrumentCategoryID(UUID.fromString(id)));
                    }
                }
        );
    }

    private PaymentInstrumentCategory convertDocToAggregate(PaymentInstrumentCategoryDocument doc){
        return new PaymentInstrumentCategory(
                new PaymentInstrumentCategoryID(UUID.fromString(doc.getPaymentInstrumentCategoryID())),
                doc.getPaymentInstrumentCategoryTypes().stream().map(
                        type -> new PaymentInstrumentType(type)
                ).collect(Collectors.toList()),
                new PaymentInstrumentCategoryName(doc.getPaymentInstrumentCategoryName())
        );
    }
}
