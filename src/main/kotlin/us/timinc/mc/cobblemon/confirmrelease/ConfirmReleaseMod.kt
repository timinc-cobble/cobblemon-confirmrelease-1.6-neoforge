package us.timinc.mc.cobblemon.confirmrelease

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import us.timinc.mc.cobblemon.confirmrelease.config.ConfigBuilder
import us.timinc.mc.cobblemon.confirmrelease.config.ConfirmReleaseConfig

@Mod(ConfirmReleaseMod.MOD_ID)
object ConfirmReleaseMod {
    const val MOD_ID = "confirmrelease"

    @Suppress("MemberVisibilityCanBePrivate")
    var config: ConfirmReleaseConfig = ConfigBuilder.load(ConfirmReleaseConfig::class.java, MOD_ID)

    var eventsLoaded = false

    val CONFIRMED_KEY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "confirmed")

    init {
        CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.subscribe { evt ->
            val pokemon = evt.pokemon
            if (config.confirmFor.any {
                    PokemonProperties.parse(it).matches(pokemon)
                }) {
                if (!pokemon.persistentData.contains(CONFIRMED_KEY.toString())) {
                    pokemon.persistentData.putBoolean(CONFIRMED_KEY.toString(), true)
                    evt.player.sendSystemMessage(Component.translatable("confirmrelease.confirmed"), false)
                    evt.cancel()
                } else {
                    evt.player.sendSystemMessage(Component.translatable("confirmrelease.released"), false)
                }
            }
        }

        CobblemonEvents.POKEMON_GAINED.subscribe { evt ->
            val pokemon = evt.pokemon
            if (!pokemon.persistentData.contains(CONFIRMED_KEY.toString())) {
                return@subscribe
            }
            pokemon.persistentData.remove(CONFIRMED_KEY.toString())
            evt.playerId.getPlayer()?.sendSystemMessage(Component.translatable("confirmrelease.unconfirmed"), false)
        }
    }
}