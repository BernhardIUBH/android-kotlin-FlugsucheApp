package com.bernhard.flugsucheapp.DetailRecyclerView

class ModelDetail(var strDeparture: String, var strArrival: String, var strCarrier: String, var dblPrice: Double) {

    companion object {
        var sortByPriceAsc = Comparator<ModelDetail> { o1, o2 -> o1.dblPrice.toInt() - o2.dblPrice.toInt() }
        var sortByPriceDesc = Comparator<ModelDetail> { o1, o2 -> o2.dblPrice.toInt() - o1.dblPrice.toInt() }

        var sortByCarrierAZ = Comparator<ModelDetail> { o1, o2 -> o1.strCarrier.compareTo(o2.strCarrier) }
        var sortByCarrierZA = Comparator<ModelDetail> { o1, o2 -> o2.strCarrier.compareTo(o1.strCarrier) }

        var sortByDateTimeDepatureAsc = Comparator<ModelDetail> { o1, o2 -> o1.strDeparture.compareTo(o2.strDeparture) }
        var sortByDateTimeDepatureDesc = Comparator<ModelDetail> { o1, o2 -> o2.strDeparture.compareTo(o1.strDeparture) }

        var sortByDateTimeArrivalAsc = Comparator<ModelDetail> { o1, o2 -> o1.strArrival.compareTo(o2.strArrival) }
        var sortByDateTimeArrivalDesc = Comparator<ModelDetail> { o1, o2 -> o2.strArrival.compareTo(o1.strArrival) }
    }
}