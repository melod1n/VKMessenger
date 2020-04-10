package ru.melod1n.vk.mvp.presenter

import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.mvp.contract.BaseContract
import ru.melod1n.vk.mvp.contract.FriendsContract
import ru.melod1n.vk.mvp.repository.FriendsRepository

class FriendsPresenter(view: BaseContract.View<VKUser>) : FriendsContract.Presenter<VKUser>(view) {

    override val tag = "FriendsPresenter"

    override val repository = FriendsRepository()
    override var loadedValues = ArrayList<VKUser>()
    override var cachedValues = ArrayList<VKUser>()
}