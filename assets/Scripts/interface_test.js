var QuestInterface = Java.type("caveyard.quest.scriptinterface.AbstractQuestInterface");
var StateInterface = Java.type("caveyard.quest.scriptinterface.DummyStateInterface");

var interface = new QuestInterface() {
    getStateInterface: function(id)
    {
        print("Hi! " + id);
        switch (id)
        {
            case 1:
                return state1;
                break;
            default:
                print("ERROR! No such state!");
        }
        return null;
    }
}

var state1 = new StateInterface() {
    enterState: function() {
        print("Hello there!");
    }
}