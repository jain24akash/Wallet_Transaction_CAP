namespace my.transactions;

using {
sap,
managed,
cuid
} from '@sap/cds/common';

entity Wallets: cuid, managed {
    balance: Decimal(9,2);
    name: String(50);
    transactions: Composition of many Transactions on transactions.wallet = $self;
};

entity Transactions: cuid, managed {
    wallet: Association to Wallets;
    amount : Decimal(9,2);
    balance: Decimal(9,2);
    description: String(256);
    transactionType: String(1);
    virtual transactionTypeText: String;
};


