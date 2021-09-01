pragma solidity >=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract BCOSLogger {
    event CreateResult(int256 count);
    event InsertResult(int256 count);
    event UpdateResult(int256 count);
    event RemoveResult(int256 count);

    TableFactory tableFactory;
    string constant TABLE_NAME = "t_bcoslogger";

    constructor() public {
        tableFactory = TableFactory(0x1001); //The fixed address is 0x1001 for TableFactory
        // the parameters of createTable are tableName,keyField,"vlaueFiled1,vlaueFiled2,vlaueFiled3,..."
        tableFactory.createTable(TABLE_NAME, "log_id", "footprint,signature");
    }

    //insert records
    function insert(
        string memory log_id,
        string memory footprint,
        string memory signature
    ) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);

        string[] memory log_id_list;

        //query existing log with exact log_id;
        (log_id_list,,) = query(log_id);
        if(log_id_list.length > 0) {
            emit InsertResult(0);
            return 0;
        }

        Entry entry = table.newEntry();
        entry.set("log_id", log_id);
        entry.set("footprint", footprint);
        entry.set("signature", signature);

        int256 count = table.insert(log_id, entry);
        emit InsertResult(count);

        return count;
    }

    //remove records
    function remove(string memory log_id, string memory footprint)
        public
        returns (int256)
    {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();
        condition.EQ("log_id", log_id);
        condition.EQ("footprint", footprint);

        int256 count = table.remove(log_id, condition);
        emit RemoveResult(count);

        return count;
    }

    //select records
    function query(string memory log_id)
        public
        view
        returns (
            string[] memory,
            string[] memory,
            string[] memory
        )
    {
        Table table = tableFactory.openTable(TABLE_NAME);

        Condition condition = table.newCondition();

        Entries entries = table.select(log_id, condition);
        string[] memory log_id_list = new string[](uint256(entries.size()));
        string[] memory footprint_list = new string[](uint256(entries.size()));
        string[] memory signature_list = new string[](uint256(entries.size()));

        for (int256 i = 0; i < entries.size(); ++i) {
            Entry entry = entries.get(i);

            log_id_list[uint256(i)] = entry.getString("log_id");
            footprint_list[uint256(i)] = entry.getString("footprint");
            signature_list[uint256(i)] = entry.getString("signature");
        }

        return (log_id_list, footprint_list, signature_list);
    }
}
