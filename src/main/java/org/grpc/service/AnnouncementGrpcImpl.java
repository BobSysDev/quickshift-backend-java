package org.grpc.service;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.grpc.entities.Announcement;
import org.grpc.entities.Employee;
import org.grpc.repositories.AnnouncementRepository;
import org.grpc.repositories.EmployeeRepository;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickshift.grpc.service.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementGrpcImpl extends AnnouncementGrpc.AnnouncementImplBase {
    private final AnnouncementRepository announcementRepository;
    private final EmployeeRepository employeeRepository;
    private final DtoConverter dtoConverter;

    public AnnouncementGrpcImpl(AnnouncementRepository announcementRepository, EmployeeRepository employeeRepository, DtoConverter dtoConverter) {
        this.announcementRepository = announcementRepository;
        this.employeeRepository = employeeRepository;
        this.dtoConverter = dtoConverter;
    }

    @Override
    public void addSingleAnnouncement(NewAnnouncementDTO request, StreamObserver<AnnouncementDTO> responseObserver) {
        Employee author = employeeRepository.findById(request.getAuthorEmployeeId());
        if (author == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.ALREADY_EXISTS_VALUE)
                    .setMessage("An employee with this working number already exists!")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        Announcement newAnnouncement = new Announcement(
                author,
                request.getTitle(),
                request.getBody(),
                dtoConverter.convertEpochMillisToLDT(request.getDateTimeOfPosting())
        );

        Announcement savedAnnouncement = announcementRepository.save(newAnnouncement);

        responseObserver.onNext(dtoConverter.convertAnnouncementToAnnouncementDTO(savedAnnouncement));
        responseObserver.onCompleted();
    }

    @Override
    public void updateSingleAnnouncement(AnnouncementDTO request, StreamObserver<AnnouncementDTO> responseObserver) {
        Announcement announcementToUpdate = announcementRepository.findById(request.getId());
        Employee author = employeeRepository.findById(request.getAuthorEmployeeId());
        if (author == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.ALREADY_EXISTS_VALUE)
                    .setMessage("An employee with this working number already exists!")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
        if (announcementToUpdate == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Announcement to update could not be found.")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        announcementToUpdate.setAuthor(author);
        announcementToUpdate.setTitle(request.getTitle());
        announcementToUpdate.setBody(request.getBody());
        announcementToUpdate.setDateTimeOfPosting(dtoConverter.convertEpochMillisToLDT(request.getDateTimeOfPosting()));

        Announcement updatedAnnouncement = announcementRepository.save(announcementToUpdate);

        responseObserver.onNext(dtoConverter.convertAnnouncementToAnnouncementDTO(updatedAnnouncement));
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAnnouncements(Empty request, StreamObserver<AnnouncementDTOList> responseObserver) {
        List<Announcement> announcementList = announcementRepository.findAll();
        List<AnnouncementDTO> announcementDTOList = new ArrayList<>();
        announcementList.forEach(announcement -> announcementDTOList.add(dtoConverter.convertAnnouncementToAnnouncementDTO(announcement)));

        responseObserver.onNext(AnnouncementDTOList.newBuilder().addAllAnnouncements(announcementDTOList).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSingleAnnouncementById(Id request, StreamObserver<AnnouncementDTO> responseObserver) {
        Announcement announcementToReturn = announcementRepository.findById(request.getId());
        if (announcementToReturn == null) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Announcement to retrieve could not be found.")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        responseObserver.onNext(dtoConverter.convertAnnouncementToAnnouncementDTO(announcementToReturn));
        responseObserver.onCompleted();
    }

    @Override //Gets the top [GenericInteger] newest announcements
    public void getMostRecentAnnouncements(GenericInteger request, StreamObserver<AnnouncementDTOList> responseObserver) {
        ArrayList<Announcement> announcementList = new ArrayList<>();
        announcementList.addAll(announcementRepository.findByOrderByDateTimeOfPostingDesc());
        List<Announcement> filteredList = announcementList.stream().limit(request.getNumber()).toList();

        List<AnnouncementDTO> announcementDTOList = new ArrayList<>();
        filteredList.forEach(announcement -> announcementDTOList.add(dtoConverter.convertAnnouncementToAnnouncementDTO(announcement)));

        responseObserver.onNext(AnnouncementDTOList.newBuilder().addAllAnnouncements(announcementDTOList).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSingleAnnouncement(Id request, StreamObserver<GenericTextMessage> responseObserver) {
        if (!announcementRepository.existsById(request.getId())) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Announcement to delete could not be found.")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }

        announcementRepository.deleteById(request.getId());
        responseObserver.onNext(GenericTextMessage.newBuilder().setText("Announcement deleted successfully.").build());
        responseObserver.onCompleted();
    }
}
