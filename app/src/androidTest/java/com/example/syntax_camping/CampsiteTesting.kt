package com.example.syntax_camping

import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert
import org.junit.Before
import org.junit.Test
/*
class CampsiteTesting {
    private lateinit var testCampsite: Campsite

    // Set up a new wizard before every single test
    @Before
    fun setUp() {
        val documentID = FirebaseFirestore.getInstance()
            .collection("campsites")
            .document()
            .id
        val mutableTags = mutableListOf<String>("yep", "tags", "you wish you had tags this cool")
        testCampsite = Campsite(documentID, "campName", "campLocation", "descriptive description", 900000000,mutableTags, 0.0 )
    }

    @Test
    fun castSpell_explosion_successful() {
        val damageDealt = evilWizard.castSpell("Explosion")

        Assert.assertEquals(30, damageDealt) // spellPower of 10 * 3 = 30

        Assert.assertEquals(20, evilWizard.mana) // mana of 30 - 10 = 20
    }

    @Test
    fun castSpell_explosion_unsuccessful() {
        evilWizard.mana = 5 // Evil Wizard does not have enough mana to cast "Explosion"
        val damageDealt = evilWizard.castSpell("Explosion")

        Assert.assertEquals(0, damageDealt)

        Assert.assertEquals(5, evilWizard.mana) // mana still is 5
    }
    @Test
    fun restoreMana_amountAddedExceedsOneHundred_manaSetToOneHundred() {
        evilWizard.restoreMana(80) // 30 + 80 = 110, so mana should be set to 100

        Assert.assertEquals(100, evilWizard.mana)
    }

    @Test
    fun restoreMana_amountAddedDoesNotExceedOneHundred_manaIncreasesCorrectly() {
        evilWizard.restoreMana(40) // 30 + 40 = 70, so mana should be set to 70

        Assert.assertEquals(70, evilWizard.mana)
    }
    @Test
    fun train_manaIsMoreThanOrEqualtoFive() {
        evilWizard.mana = 5 // 30 + 40 = 70, so mana should be set to 70
        val priorSpellAttackPower = evilWizard.spellPower

        evilWizard.train()
        assertEquals(priorSpellAttackPower+1, evilWizard.spellPower)
        Assert.assertEquals(0, evilWizard.mana)
    }
    @Test
    fun train_manaIsLessThanFive() {
        evilWizard.mana = 4 // 30 + 40 = 70, so mana should be set to 70
        val priorSpellAttackPower = evilWizard.spellPower
        evilWizard.train()
        assertEquals(priorSpellAttackPower, evilWizard.spellPower)
    }
}*/