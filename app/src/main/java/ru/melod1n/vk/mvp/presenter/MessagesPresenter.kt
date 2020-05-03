package ru.melod1n.vk.mvp.presenter

import ru.melod1n.vk.api.model.VKMessage
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.repository.MessagesRepository

class MessagesPresenter(private val view: BaseContract.View<VKMessage>) : BaseContract.Presenter<VKMessage>(view) {

    override val tag = "MessagesPresenter"

    override val repository = MessagesRepository()

    override var loadedValues = ArrayList<VKMessage>()
    override var cachedValues = ArrayList<VKMessage>()
}