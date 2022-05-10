using {my.transactions as my} from '../db/wallet';



service WalletService {
    entity Wallets as projection on my.Wallets;

    entity Transactions as projection on my.Transactions {
        *,
        case Transactions.transactionType
            when 'C'
            then 'Credit'
            when 'D'
            then 'Debit'
        end as transactionTypeText: String
    };
};