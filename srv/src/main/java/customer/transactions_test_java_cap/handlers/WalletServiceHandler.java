package customer.transactions_test_java_cap.handlers;

import com.sap.cds.CdsData;
import com.sap.cds.Result;
import com.sap.cds.Row;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnAnalyzer;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.reflect.CdsModel;
import com.sap.cds.services.messages.Messages;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@ServiceName("WalletService")
public class WalletServiceHandler implements EventHandler {

    private final PersistenceService db;

    private final Messages messages;

    private final CqnAnalyzer analyzer;

    WalletServiceHandler(PersistenceService db,
                        Messages messages, CdsModel model) {
        this.db = db;
        this.messages = messages;

        // model is a tenant-dependant model proxy
        this.analyzer = CqnAnalyzer.create(model);
    }



    @Before(event = CdsService.EVENT_CREATE, entity = "WalletService.Transactions")
    public void BeforeCreate(CdsCreateEventContext context){
        System.out.println("In before create of transaction Handler");
        // System.out.println(context.);
        context.getCqn().entries().forEach(transaction -> {
            // System.out.println(transaction);
            float amount = ((BigDecimal) transaction.get("amount")).floatValue();
            if (amount == 0) {
                throw new ServiceException(ErrorStatuses.BAD_REQUEST, "Amount cannot be 0");
            }
            if (amount > 0) {
                transaction.put("transactionType", "C" );
            } else {
                transaction.put("transactionType", "D" );
            }

        });

    }

    @Transactional()
    @On(event = CdsService.EVENT_CREATE, entity = "WalletService.Transactions")
    public void onCreate(CdsCreateEventContext context, List<CdsData> data){
        System.out.println(data.get(0));
        System.out.println(context.getCqn().entries());
        Result result = db.run(Select.from("WalletService.Wallets").byId(data.get(0).get("wallet_ID")));
        float updBalance=0;
        for(Row row: result){
            updBalance = ((BigDecimal) row.get("balance")).floatValue() + ((BigDecimal) data.get(0).get("amount")).floatValue();
//            row.put("balance", updBalance);
        }
        data.get(0).put("balance", updBalance);
        System.out.println(result);
        System.out.println("In on create of transaction Handler");
        db.run(Update.entity("WalletService.Wallets").data("balance", updBalance).byId(data.get(0).get("wallet_ID")));
        // throw new ServiceException(ErrorStatuses.BAD_REQUEST,"Dead end");
    }

}
