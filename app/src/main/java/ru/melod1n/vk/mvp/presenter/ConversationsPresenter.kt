package ru.melod1n.vk.mvp.presenter

import ru.melod1n.vk.api.model.VKConversation
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.repository.ConversationsRepository

class ConversationsPresenter(view: BaseContract.View<VKConversation>) : BaseContract.Presenter<VKConversation>(view) {

    override val tag = "ConversationsPresenter"

    override val repository = ConversationsRepository()

    override var loadedValues = ArrayList<VKConversation>()
    override var cachedValues = ArrayList<VKConversation>()
}